
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef CEIL_UF_H
#define CEIL_UF_H
; 
float ceil_uf(float x){
    return ceil(x);; 
}

#endif
 ; 
void ceil(float * v_initial_param_261_136, float * & v_user_func_263_137, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_263_137 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_135 = 0;(v_i_135 <= (-1 + v_N_0)); (++v_i_135)){
        v_user_func_263_137[v_i_135] = ceil_uf(v_initial_param_261_136[v_i_135]); 
    }
}
}; 