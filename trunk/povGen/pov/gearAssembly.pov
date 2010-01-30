union {
	object{ GearInv ( 80, 0.15, 0.5) 
	        texture { ac3d_col_3 }
	        rotate<0,clock*360/20,0>
	}
	
	object{ Gear ( 20, 0.15, 0.5) 
	        texture { ac3d_col_6 }
	        rotate<0,2*clock*360/20,0>
	}
	
	union{
		union{
		object{ Gear (30, 0.15, 0.45) }
			cylinder { <0,0,0>,<0,-30/16,0>,.15 } 
				    texture { ac3d_col_9 }
		    texture { ac3d_col_9 }
		    rotate<0,2*clock*360/20,0>
		    translate<Gear_Radius(20,0.15)+Gear_Radius(30,0.15),0,0.025>
		}
		union{
		object{ Gear (30, 0.15, 0.45) 
		}
		cylinder { <0,0,0>,<0,-30/16,0>,.15 } 
				    texture { ac3d_col_9 }
		    texture { ac3d_col_9 }
		    rotate<0,2*clock*360/20,0>
		    translate<-Gear_Radius(20,0.15)-Gear_Radius(30,0.15),0,0.025>
		}
		union{
		object{ Gear (30, 0.15, 0.45) 
		}
		cylinder { <0,0,0>,<0,-30/16,0>,.15 } 
				    texture { ac3d_col_9 }
		    texture { ac3d_col_9 }
		    rotate<0,2*clock*360/20,0>
		    translate<0,0,Gear_Radius(20,0.15)+Gear_Radius(30,0.15)>
		}
		union{
		object{ Gear (30, 0.15, 0.45) 
		}
		cylinder { <0,0,0>,<0,-30/16,0>,.15 } 
				    texture { ac3d_col_9 }
		    rotate<0,2*clock*360/20,0>
		    translate<0,0,-Gear_Radius(20,0.15)-Gear_Radius(30,0.15)>
		}
	    rotate<0,2*clock*360/20,0>
	}
	scale <16,16,16>
	rotate<90,90,0>
}