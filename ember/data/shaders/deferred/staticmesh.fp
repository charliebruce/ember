#version 150

//Deferred shader - fragment

varying vec2 texcoord;

uniform sampler2D albedomap;
uniform sampler2D normalmap;
uniform sampler2D alphamap;
uniform sampler2D specularmap;

uniform float specmultiplier=1.0f;

varying vec3 vcolour;
varying vec3 norm;
varying vec4 tangent;

void main(){

	//float alphaval = texture2D(alphamap, texcoord).x;
	
	//if(alphaval<0.5){//For alpha=0
		//discard;
	//}
	
	vec4 diffusecolour = texture2D(albedomap, texcoord).rgba;
	vec3 mapnorm = texture2D(normalmap, texcoord).xyz;
	vec3 bumpmapnorm = normalize((mapnorm.xyz*2.0)-1.0);
	
	float specpower = texture2D(specularmap, texcoord).x*specmultiplier;
	specpower=50.0f;
	vec3 N = normalize(norm);							//Normal (as according to geometry)
	vec3 T = normalize(tangent.xyz - dot(tangent.xyz, N)*N);	//Tangent - this calc is already done in Chunk generator!
	vec3 B = cross(N,T)*tangent.w;								//Binormal
	
	mat3 TBN = mat3(T, B, N);
	
	vec3 bumpednormal = normalize(TBN*bumpmapnorm);  //Resultant, adjusted normal.
	
	vec3 renorm = bumpednormal*0.5+0.5;//norm
	//vec3 renorm = N*0.5+0.5;//norm
	
	gl_FragData[0] = vec4(diffusecolour.xyz, 1.0);//Specular power into Alpha? Limit to 256?TODO blending of albedo?
	gl_FragData[1] = vec4(renorm,1.0);//Experiment c b norm or adjnorm
	gl_FragData[2] = vec4(0.5125,0.75,0.875,1.0);
	
}