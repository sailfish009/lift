
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef LDEXP_UF_H
#define LDEXP_UF_H
; 
float ldexp_uf(float x, float y){
    return x* pow(2,y) ;; 
}

#endif
 ; 
void ldexp(float * v_initial_param_589_257, float * v_initial_param_590_258, float * & v_user_func_596_260, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_596_260 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_256 = 0;(v_i_256 <= (-1 + v_N_0)); (++v_i_256)){
        v_user_func_596_260[v_i_256] = ldexp_uf(v_initial_param_589_257[v_i_256], v_initial_param_590_258[v_i_256]); 
    }
}
}; 