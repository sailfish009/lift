
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef EXP_UF_H
#define EXP_UF_H
; 
float exp_uf(float x){
    return exp(x) ;; 
}

#endif
 ; 
void lift_exp(float * v_initial_param_425_172, float * & v_user_func_427_173, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_427_173 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_171 = 0;(v_i_171 <= (-1 + v_N_0)); (++v_i_171)){
        v_user_func_427_173[v_i_171] = exp_uf(v_initial_param_425_172[v_i_171]); 
    }
}
}; 