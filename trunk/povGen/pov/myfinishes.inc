#declare Metal_Finish =
finish {
    metallic
//    ambient 0.2
//    diffuse 0.1
    brilliance 6
//    reflection .01
    phong 0.75
    phong_size 80
}

#declare Chrome_Finish =
    finish {
        ambient 0.3
        diffuse 0.7
        reflection 0.01
        brilliance 8
        specular 0.9
        roughness 0.1
    }
    
#declare Polished_Chrome_Finish = 
    finish {
        ambient 0.1
        diffuse 0.7
        brilliance 6.0
        reflection 0.2
        phong 0.8
        phong_size 12
		roughness 0.1
        
    }
