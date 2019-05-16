
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
void nancumprod(float * v_initial_param_1569_327, float * & v_user_func_1572_328, int v_N_190){
    // Allocate memory for output pointers
    v_user_func_1572_328 = reinterpret_cast<float *>(malloc((v_N_190 * sizeof(float)))); 
    // For each element scanned sequentially
    float scan_acc_1579 = 1.0f;
    for (int v_i_326 = 0;(v_i_326 <= (-1 + v_N_190)); (++v_i_326)){
        scan_acc_1579 = prod2_uf(scan_acc_1579, v_initial_param_1569_327[v_i_326]); 
        v_user_func_1572_328[v_i_326] = scan_acc_1579; 
    }
}
}; 