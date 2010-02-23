// Macro for the adjustment of images
// for image_map with assumed_gamma = 1.0 ;
#macro Correct_Pigment_Gamma(Orig_Pig, New_G)
  #local Correct_Pig_fn =
      function{ pigment {Orig_Pig} }
  pigment{ average pigment_map{
   [function{ pow(Correct_Pig_fn(x,y,z).x, New_G)}
               color_map{[0 rgb 0][1 rgb<3,0,0>]}]
   [function{ pow(Correct_Pig_fn(x,y,z).y, New_G)}
               color_map{[0 rgb 0][1 rgb<0,3,0>]}]
   [function{ pow(Correct_Pig_fn(x,y,z).z, New_G)}
               color_map{[0 rgb 0][1 rgb<0,0,3>]}]
   }}
#end //

plane{<0,1,0>, 0
     texture{Polished_Chrome
              pigment{ color rgb<1,0.8,0>}
              normal { crackle 0.15 turbulence 0.15 scale 0.25}
              finish { diffuse 0.0}
              }
     }

sky_sphere{
   Correct_Pigment_Gamma( // gamma correction
     pigment{
     image_map{ jpeg "pov/sky_map_p_04_5200x1300.jpg"
                map_type 2    //  cylindrical
                interpolate 2 //  bilinear
                once //
              } 
      scale<1,1.02,1> rotate<0,0.00,0>
    } 
    , 2.2)
}