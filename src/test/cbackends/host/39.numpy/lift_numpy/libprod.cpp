
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef PROD2_UF_H
#define PROD2_UF_H
; 
float prod2_uf(float l, float r){
    { return (l * r); }; 
}

#endif
 ; 
void prod(float * v_initial_param_1545_309, float * & v_user_func_1548_310, int v_N_190){
    // Allocate memory for output pointers
    v_user_func_1548_310 = reinterpret_cast<float *>(malloc((1 * sizeof(float)))); 
    // For each element reduced sequentially
    v_user_func_1548_310[0] = 1.0f; 
    for (int v_i_308 = 0;(v_i_308 <= (-1 + v_N_190)); (++v_i_308)){
        v_user_func_1548_310[0] = prod2_uf(v_user_func_1548_310[0], v_initial_param_1545_309[v_i_308]); 
    }
}
}; 