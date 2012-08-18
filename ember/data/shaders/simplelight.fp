#version 150

uniform sampler2D normalBuffer;
uniform sampler2D depthBuffer;

void main(){
gl_FragData[0] = vec4(0.3f,0.5f,0.7f,1.0f);
}