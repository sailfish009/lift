


#include <bits/stdc++.h>

using namespace std;

namespace lift {
    


#ifndef HYPOT_UF_H
#define HYPOT_UF_H


; 
float hypot_uf(float x, float y){
    
    
    { return sqrt((x*x)+(y*y)); }
    
    ; 
}



#endif
; 
void hypot(float * v_initial_param_108_37, float * v_initial_param_109_38, float * & v_user_func_115_40, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_115_40 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_36 = 0;(v_i_36 <= (-1 + v_N_0)); (++v_i_36)){
        v_user_func_115_40[v_i_36] = hypot_uf(v_initial_param_108_37[v_i_36], v_initial_param_109_38[v_i_36]); 
    }
}


}

; 