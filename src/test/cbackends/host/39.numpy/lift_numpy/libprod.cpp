
#include <bits/stdc++.h>

using namespace std;

namespace lift {
    
#ifndef PROD2_H
#define PROD2_H
; 
float prod2(float l, float r){
    { return (l * r); }; 
}

#endif; 
void prod(float * v_initial_param_255_112, float * & v_user_func_258_113, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_258_113 = reinterpret_cast<float *>(malloc((1 * sizeof(float)))); 
    // For each element reduced sequentially
    v_user_func_258_113[0] = 1.0f; 
    for (int v_i_111 = 0;(v_i_111 <= (-1 + v_N_0)); (++v_i_111)){
        v_user_func_258_113[0] = prod2(v_user_func_258_113[0], v_initial_param_255_112[v_i_111]); 
    }
}
}; 