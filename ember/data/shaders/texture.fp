#version 120


//The input texture
uniform sampler2D tex;


//Texture point on the sampler2D
varying vec2 textureCoord;


void main(){
gl_FragColor = texture2D(tex,textureCoord);
}