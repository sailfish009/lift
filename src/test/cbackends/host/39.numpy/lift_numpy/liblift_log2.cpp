
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef LOG2_UF_H
#define LOG2_UF_H
; 
float log2_uf(float x){
    return log2(x) ;; 
}

#endif
 ; 
void lift_log2(float * v_initial_param_478_207, float * & v_user_func_480_208, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_480_208 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_206 = 0;(v_i_206 <= (-1 + v_N_0)); (++v_i_206)){
        v_user_func_480_208[v_i_206] = log2_uf(v_initial_param_478_207[v_i_206]); 
    }
}
}; 