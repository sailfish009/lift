
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef FLOOR_UF_H
#define FLOOR_UF_H
; 
float floor_uf(float x){
    return floor(x);; 
}

#endif
 ; 
void lift_floor(float * v_initial_param_262_141, float * & v_user_func_264_142, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_264_142 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_140 = 0;(v_i_140 <= (-1 + v_N_0)); (++v_i_140)){
        v_user_func_264_142[v_i_140] = floor_uf(v_initial_param_262_141[v_i_140]); 
    }
}
}; 