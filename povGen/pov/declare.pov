#version 3.6;
global_settings {  assumed_gamma 1.3 }
global_settings { max_trace_level 10 }//(1...20) [default = 5]
#include "colors.inc"
#include "textures.inc"
#include "golds.inc"

#declare ac3d_col_0 = texture { pigment { color rgbf < 0.286 0.278 0.247 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_1 = texture { pigment { color rgbf < 0.412 0.435 0.416 0>} finish { ambient 0 specular 0.2 roughness 0.005} }
#declare ac3d_col_2 = texture { pigment { color rgbf < 0 0 1 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_3 = texture { pigment { color rgbf < 1 0 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_4 = texture { pigment { color rgbf < 0.722 0 0.71 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_5 = texture { pigment { color rgbf < 1 1 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_6 = texture { pigment { color rgbf < 0 1 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_7 = texture { pigment { color rgbf < 0.588 0.149 0.286 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_8 = texture { pigment { color rgbf < 0.627451 0.752941 0.878431 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_9 = texture { pigment { color rgbf < 1 0.365 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_10 = texture { pigment { color rgbf < 0.671 0.169 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_11 = texture { pigment { color rgbf < 0.933333 0.501961 0.933333 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_12 = texture { pigment { color rgbf < 0.266667 0.266667 0.266667 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_13 = texture { pigment { color rgbf < 0.533333 0.533333 0.533333 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_14 = texture { pigment { color rgbf < 0.8 0.8 0.8 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_15 = texture { pigment { color rgbf < 1 0 0 0>} finish { ambient 0.2 specular 0.2 roughness 0.265625} }
#declare ac3d_col_16 = texture { pigment { color rgbf < 0.635 0.141 0.616 0>} finish { ambient 0.2 specular 0.2 roughness 0.005} }
#declare ac3d_col_17 = texture { pigment { color rgbf < 0.192 1 0.2 0>} finish { ambient 0 specular 0.2 roughness 0.890625} }
#declare ac3d_col_18 = texture { pigment { color rgbf < 0.271 0.522 0.129 0>} finish { ambient 0 specular 0.2 roughness 1} }
#declare ac3d_col_19 = texture { pigment { color rgbf < 1 1 1 0>} finish { ambient 0 specular 0 roughness 1} }
#declare ac3d_col_20 = texture { pigment { color rgbf < 0.173 0.18 1 0>} finish { ambient 0 specular 0 roughness 1} }
#declare ac3d_col_21 = texture { pigment { color rgbf < 0 0.192 1 0>} finish { ambient 0 specular 0 roughness 1} }
#declare ac3d_col_22 = texture { pigment { color rgbf < 1 0.086 0.106 0>} finish { ambient 0 specular 0 roughness 1} }
#declare ac3d_col_23 = texture { pigment { color rgbf < 1 0.18 0.161 0>} finish { ambient 0 specular 0 roughness 1} }

