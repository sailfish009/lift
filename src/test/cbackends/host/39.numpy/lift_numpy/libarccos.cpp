


#include <bits/stdc++.h>

using namespace std;

namespace lift {
    


#ifndef ARCCOS_UF_H
#define ARCCOS_UF_H


; 
float arccos_uf(float x){
    
    
    { return acos(x); }
    
    ; 
}



#endif
; 
void arccos(float * v_initial_param_100_37, float * & v_user_func_102_38, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_102_38 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_36 = 0;(v_i_36 <= (-1 + v_N_0)); (++v_i_36)){
        v_user_func_102_38[v_i_36] = arccos_uf(v_initial_param_100_37[v_i_36]); 
    }
}


}

; 