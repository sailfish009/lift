
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef ADD_H
#define ADD_H
; 
float add(float l, float r){
    { return (l + r); }; 
}

#endif
 ; 
void nancumsum(float * v_initial_param_314_166, float * & v_user_func_317_167, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_317_167 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element scanned sequentially
    float scan_acc_324 = 0.0f;
    for (int v_i_165 = 0;(v_i_165 <= (-1 + v_N_0)); (++v_i_165)){
        scan_acc_324 = add(scan_acc_324, v_initial_param_314_166[v_i_165]); 
        v_user_func_317_167[v_i_165] = scan_acc_324; 
    }
}
}; 