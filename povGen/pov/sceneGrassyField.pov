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

plane { <0,1,0>, 0
       texture{ pigment{color rgb<0.35,0.65,0.0>*0.9}
                normal {bumps 0.75 scale 0.015}
                finish {ambient 0.1 diffuse 0.8}
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