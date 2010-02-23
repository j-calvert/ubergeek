sky_sphere{
 pigment{ gradient <0,1,0>
          color_map{
          [0.0 color rgb<1,1,1>        ]
          [0.8 color rgb<0.1,0.25,0.75>]
          [1.0 color rgb<0.1,0.25,0.75>]}
        }
 }
 
 plane { <0,1,0>, 0
       texture{ pigment{color rgb<0.35,0.65,0.0>*0.9}
                normal {bumps 0.75 scale 0.015}
                finish {ambient 0.1 diffuse 0.8}
              }
     }

 