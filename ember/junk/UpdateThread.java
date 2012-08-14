// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UpdateThread.java

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

class UpdateThread extends Thread
{

    public UpdateThread(String appDir, String codeBase, String appName)
    {
        quit = false;
        status = "";
        progress = 0;
        exception = null;
        if(!appDir.endsWith("/") && !appDir.endsWith("\\"))
            appDir = (new StringBuilder(String.valueOf(appDir))).append("\\").toString();
        this.appDir = appDir;
        this.codeBase = codeBase;
        this.appName = appName;
    }

    public void abort()
    {
        quit = true;
    }

    public void run()
    {
        File file = new File(appDir);
        if(!file.exists() && !file.mkdir())
            throw new Exception("Could not find application directory");
        status = "Registering Uninstaller";
        progress = 4;
        Process process = Runtime.getRuntime().exec(new String[] {
            "javaw", "-cp", (new StringBuilder(String.valueOf(appDir))).append("bootstrap.jar").toString(), (new StringBuilder("-Djava.library.path=")).append(appDir).toString(), "ControlPanel", "-add", appName, appName, appDir
        }, null, new File(appDir));
        process.waitFor();
        if(quit)
            return;
        FileList fileList;
        status = "Getting list of installed files";
        progress = 6;
        try
        {
            fileList = FileList.load(appDir);
        }
        catch(Throwable t)
        {
            fileList = new FileList();
        }
        for(Iterator it = fileList.entries.keySet().iterator(); it.hasNext();)
        {
            String fileName = (String)it.next();
            if(!(new File((new StringBuilder(String.valueOf(appDir))).append(fileName).toString())).exists())
                it.remove();
        }

        if(quit)
            return;
        ArrayList manifest;
        InputStream is;
        BufferedReader br;
        status = "Downloading manifest";
        progress = 8;
        URL url = new URL((new StringBuilder(String.valueOf(codeBase))).append("/manifest.txt").toString());
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
        urlc.setDoOutput(false);
        urlc.setDoInput(true);
        urlc.setUseCaches(false);
        manifest = new ArrayList();
        is = urlc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        int i;
        byte buffer[] = new byte[2048];
        i = 0;
          goto _L1
_L3:
        if(quit)
        {
            is.close();
            return;
        }
        String line;
        line = line.trim();
        if(line.length() != 0)
            if(i == 0)
                commandLine = line;
            else
                manifest.add(line);
        i++;
_L1:
        if((line = br.readLine()) != null) goto _L3; else goto _L2
_L2:
        break MISSING_BLOCK_LABEL_499;
        Exception exception1;
        exception1;
        is.close();
        throw exception1;
        Iterator it;
        is.close();
        progress = 10;
        status = "Looking for old files...";
        it = fileList.entries.entrySet().iterator();
          goto _L4
_L6:
        String fileName;
        io.FileList.Entry fileEntry;
        java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
        fileName = (String)entry.getKey();
        fileEntry = (io.FileList.Entry)entry.getValue();
        if(quit)
            return;
        if(fileEntry.dynamic)
            continue; /* Loop/switch isn't completed */
        for(int i = 0; i < manifest.size(); i++)
        {
            String fileNameManifest = (String)manifest.get(i);
            if(fileNameManifest.equalsIgnoreCase(fileName))
                continue; /* Loop/switch isn't completed */
        }

        status = (new StringBuilder("Removing ")).append(fileName).toString();
        (new File((new StringBuilder(String.valueOf(appDir))).append(fileName).toString())).delete();
        it.remove();
        fileList.save(appDir);
        it = fileList.entries.entrySet().iterator();
_L4:
        if(it.hasNext()) goto _L6; else goto _L5
_L5:
        long bytesToDownload;
        bytesToDownload = 0L;
        fileName = manifest.size() - 1;
          goto _L7
_L9:
        String fileName;
        fileName = (String)manifest.get(fileName);
        status = (new StringBuilder("Inspecting ")).append(fileName).toString();
        if(quit)
            return;
        URL url = new URL((new StringBuilder(String.valueOf(codeBase))).append("/").append(fileName).toString());
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
        urlc.setDoOutput(false);
        urlc.setDoInput(true);
        urlc.setRequestMethod("HEAD");
        urlc.setUseCaches(false);
        String etag = urlc.getHeaderField("ETAG");
        if(etag == null)
            throw new Exception((new StringBuilder("Could not determine if ")).append(fileName).append(" was modified.").toString());
        if(urlc.getContentLength() == -1)
            throw new Exception((new StringBuilder("Could not determine the file size of ")).append(fileName).append(".").toString());
        io.FileList.Entry fileEntry = (io.FileList.Entry)fileList.entries.get(fileName);
        if(fileEntry != null && fileEntry.hash.equals(etag))
            manifest.remove(fileName);
        else
            bytesToDownload += urlc.getContentLength();
        fileName--;
_L7:
        if(fileName >= 0) goto _L9; else goto _L8
_L8:
        long startTime;
        long bytesDownloaded;
        int i;
        startTime = System.currentTimeMillis();
        bytesDownloaded = 0L;
        i = 0;
          goto _L10
_L15:
        String fileName;
        fileName = (String)manifest.get(i);
        if(quit)
            return;
        io.FileList.Entry fileEntry;
        String etag;
        FileOutputStream fos;
        fileEntry = new io.FileList.Entry();
        fileEntry.hash = "?";
        fileEntry.lastUsed = 0L;
        fileEntry.dynamic = false;
        fileList.entries.put(fileName, fileEntry);
        fileList.save(appDir);
        URL url = new URL((new StringBuilder(String.valueOf(codeBase))).append("/").append(fileName).toString());
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
        urlc.setDoOutput(false);
        urlc.setDoInput(true);
        urlc.setUseCaches(false);
        etag = urlc.getHeaderField("ETAG");
        if(etag == null)
            throw new Exception((new StringBuilder("Could not determine if ")).append(fileName).append(" was modified.").toString());
        fos = new FileOutputStream((new StringBuilder(String.valueOf(appDir))).append(fileName).toString());
        is = urlc.getInputStream();
        byte tmp[] = new byte[1024];
          goto _L11
_L13:
        if(quit)
        {
            fos.close();
            is.close();
            return;
        }
        int len;
        fos.write(tmp, 0, len);
        bytesDownloaded += len;
        progress = (int)(10L + (bytesDownloaded * 89L) / bytesToDownload);
        String newStatus = (new StringBuilder("Downloading ")).append(toMB(bytesDownloaded)).append("/").append(toMB(bytesToDownload)).append(" MB").toString();
        int elapsedSeconds = (int)(System.currentTimeMillis() - startTime) / 1000;
        if(elapsedSeconds > 1)
            newStatus = (new StringBuilder(String.valueOf(newStatus))).append(" (").append(toKB(bytesDownloaded / (long)elapsedSeconds)).append(" kB/s)").toString();
        status = newStatus;
_L11:
        if((len = is.read(tmp)) != -1) goto _L13; else goto _L12
_L12:
        break MISSING_BLOCK_LABEL_1441;
        Exception exception2;
        exception2;
        fos.close();
        is.close();
        throw exception2;
        fos.close();
        is.close();
        fileEntry.hash = etag;
        fileList.save(appDir);
        i++;
_L10:
        if(i < manifest.size()) goto _L15; else goto _L14
_L14:
        status = "";
        progress = 100;
        break MISSING_BLOCK_LABEL_1548;
        Exception e;
        e;
        exception = e;
        break MISSING_BLOCK_LABEL_1548;
        Throwable t;
        t;
        exception = new Exception((new StringBuilder(String.valueOf(t.getClass().getName()))).append(": ").append(t.getMessage()).toString());
    }

    private String toMB(long numBytes)
    {
        int mb = (int)((numBytes * 10L) / 0x100000L);
        return (new StringBuilder()).append(mb / 10).append(".").append(mb % 10).toString();
    }

    private String toKB(long numBytes)
    {
        int kb = (int)((numBytes * 10L) / 1024L);
        return (new StringBuilder()).append(kb / 10).append(".").append(kb % 10).toString();
    }

    private void unpack(String fileName, String appDir)
        throws Exception
    {
        InputStream is;
        FileOutputStream fos;
        is = UpdateThread.getResourceAsStream(fileName);
        if(is == null)
            throw new FileNotFoundException(fileName);
        fos = new FileOutputStream((new StringBuilder(String.valueOf(appDir))).append(fileName).toString());
        byte buffer[] = new byte[1024];
        int numBytes;
        while((numBytes = is.read(buffer)) > 0) 
            fos.write(buffer, 0, numBytes);
        break MISSING_BLOCK_LABEL_92;
        Exception exception1;
        exception1;
        fos.close();
        throw exception1;
        fos.close();
        return;
    }

    public String getStatus()
        throws Exception
    {
        if(exception != null)
            throw exception;
        else
            return status;
    }

    public int getProgress()
        throws Exception
    {
        if(exception != null)
            throw exception;
        else
            return progress;
    }

    public String getCommandLine()
        throws Exception
    {
        if(exception != null)
            throw exception;
        else
            return commandLine;
    }

    private final String codeBase;
    private final String appDir;
    private final String appName;
    private boolean quit;
    private String status;
    private int progress;
    private Exception exception;
    private String commandLine;
}
