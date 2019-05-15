
#include <bits/stdc++.h>

using namespace std;

namespace lift {
    
#ifndef ARCSINH_UF_H
#define ARCSINH_UF_H
; 
float arcsinh_uf(float x){
    { return asinh(x); }; 
}

#endif; 
void arcsinh(float * v_initial_param_192_87, float * & v_user_func_194_88, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_194_88 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_86 = 0;(v_i_86 <= (-1 + v_N_0)); (++v_i_86)){
        v_user_func_194_88[v_i_86] = arcsinh_uf(v_initial_param_192_87[v_i_86]); 
    }
}
}; 