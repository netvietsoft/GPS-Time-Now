precision highp float;
varying vec2 vTextureCoord;
uniform sampler2D uInputTexture;
uniform vec2 uSingleStepOffset;
uniform float uSkinSmoothLevel;
uniform float uWhitenLevel;

void main() {
    vec4 centralColor = texture2D(uInputTexture, vTextureCoord);
    if (uSkinSmoothLevel <= 0.01 && uWhitenLevel <= 0.01) {
        gl_FragColor = centralColor;
        return;
    }
    vec3 sum = centralColor.rgb * 0.36;
    sum += texture2D(uInputTexture, vTextureCoord + uSingleStepOffset * vec2( 1.5,  1.5)).rgb * 0.16;
    sum += texture2D(uInputTexture, vTextureCoord + uSingleStepOffset * vec2(-1.5,  1.5)).rgb * 0.16;
    sum += texture2D(uInputTexture, vTextureCoord + uSingleStepOffset * vec2( 1.5, -1.5)).rgb * 0.16;
    sum += texture2D(uInputTexture, vTextureCoord + uSingleStepOffset * vec2(-1.5, -1.5)).rgb * 0.16;

    vec3 smoothed = mix(centralColor.rgb, sum, clamp(uSkinSmoothLevel * 0.65, 0.0, 1.0));
    vec3 whitened = clamp(smoothed + vec3(uWhitenLevel * 0.08), 0.0, 1.0);
    gl_FragColor = vec4(whitened, centralColor.a);
}
