
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef SQUARE_UF_H
#define SQUARE_UF_H
; 
float square_uf(float x){
    { return x*x; }; 
}

#endif
 ; 
void square(float * v_initial_param_887_388, float * & v_user_func_889_389, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_889_389 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_387 = 0;(v_i_387 <= (-1 + v_N_0)); (++v_i_387)){
        v_user_func_889_389[v_i_387] = square_uf(v_initial_param_887_388[v_i_387]); 
    }
}
}; 