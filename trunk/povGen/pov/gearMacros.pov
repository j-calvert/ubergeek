#macro Gear (Cog_Number, Cog_Distance, Gear_Z) // around the y axis 
#local D = 0.0001;
#local Gear_Circumference = Cog_Distance*Cog_Number;
#local Gear_Radius_ = Gear_Circumference/(2*pi); 
 
union{
 // kernel of the gear minus notches
difference{
 cylinder { <0,0,0>,<0,Gear_Z,0>,Gear_Radius_ 
           scale <1,1,1> rotate<0,0,0> translate<0,0,0>
         } // end of cylinder
          torus { Gear_Radius_/2,Gear_Radius_/4  
         scale <1,0.1,1> translate<0,0,0>
       } // end of torus  -------------------------------              
 torus { Gear_Radius_/2,Gear_Radius_/4  
         scale <1,0.1,1> translate<0,Gear_Z,0>
       } // end of torus  ------------------------------- 
 #local Nr = 0;     // start
 #local EndNr = Cog_Number; // end
 #while (Nr< EndNr) 
    cylinder { <0,0-D,0>,<0,Gear_Z+D,0>,Cog_Distance/4
               scale<1.5,1,1>
               translate<Gear_Radius_,0,0> 
               rotate<0,(Nr+0.5) * 360/EndNr,0>  
               } // end of cylinder -----------------------------------
 #local Nr = Nr + 1;    // next Nr
 #end // ---------------  end of loop 
          } // end of difference ---------------
 // adding the cogs
 #local Nr = 0;     // start
 #local EndNr = Cog_Number; // end
 #while (Nr< EndNr) 
    cylinder { <0,0,0>,<0,Gear_Z,0>,Cog_Distance/4
               scale<1.3,1,1>
               translate<Gear_Radius_,0,0> 
               rotate<0,Nr * 360/EndNr,0>  
               } // end of cylinder -----------------------------------
 #local Nr = Nr + 1;    // next Nr
 #end // ---------------  end of loop 
 cylinder { <0,-0.05,0>,<0,Gear_Z+0.05,0>,Gear_Radius_/4 
           scale <1,1,1> rotate<0,0,0> translate<0,0,0>
         } // end of cylinder

 cylinder { <0,-0.10,0>,<0,Gear_Z+0.10,0>,Gear_Radius_/10 
           scale <1,1,1> rotate<0,0,0> translate<0,0,0>
          } // end of cylinder



} #end // end of union and end of macro -------------------------------
        
        
#macro GearInv (Cog_Number, Cog_Distance, Gear_Z) // around the y axis 
#local D = 0.0001;
#local Gear_Circumference = Cog_Distance*Cog_Number;
#local Gear_Radius_ = Gear_Circumference/(2*pi); 
 
union{
 // kernel of the gear minus notches
difference{
 cylinder { <0,0,0>,<0,Gear_Z,0>,Gear_Radius_+.2
           scale <1,1,1> rotate<0,0,0> translate<0,0,0>
 }
cylinder { <0,0,0>,<0,Gear_Z,0>,Gear_Radius_
           scale <1,1.5,1> rotate<0,0,0> translate<0,-.2,0>
         } // end of cylinder
 #local Nr = 0;     // start
 #local EndNr = Cog_Number; // end
 #while (Nr< EndNr) 
    cylinder { <0,0-D,0>,<0,Gear_Z+D,0>,Cog_Distance/4
               scale<1.5,1,1>
               translate<Gear_Radius_,0,0> 
               rotate<0,(Nr+0.5) * 360/EndNr,0>  
               } // end of cylinder -----------------------------------
 #local Nr = Nr + 1;    // next Nr
 #end // ---------------  end of loop 
          } // end of difference ---------------
 // adding the cogs
 #local Nr = 0;     // start
 #local EndNr = Cog_Number; // end
 #while (Nr< EndNr) 
    cylinder { <0,0,0>,<0,Gear_Z,0>,Cog_Distance/4
               scale<1.3,1,1>
               translate<Gear_Radius_,0,0> 
               rotate<0,Nr * 360/EndNr,0>  
               } // end of cylinder -----------------------------------
 #local Nr = Nr + 1;    // next Nr
 #end // ---------------  end of loop 
} #end // end of union and end of macro -------------------------------

#macro Gear_Radius (Cog_Number, Cog_Distance)  //----------------------
  Cog_Distance*Cog_Number/(2*pi)
#end //----------------------------------------------------------------


