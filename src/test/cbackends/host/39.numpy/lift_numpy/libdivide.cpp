
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef DIVIDE_UF_H
#define DIVIDE_UF_H
; 
float divide_uf(float x, float y){
    return x / y;; 
}

#endif
 ; 
void divide(float * v_initial_param_684_308, float * v_initial_param_685_309, float * & v_user_func_691_311, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_691_311 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_307 = 0;(v_i_307 <= (-1 + v_N_0)); (++v_i_307)){
        v_user_func_691_311[v_i_307] = divide_uf(v_initial_param_684_308[v_i_307], v_initial_param_685_309[v_i_307]); 
    }
}
}; 