
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef DIFF2_H
#define DIFF2_H
; 
float diff2(float l, float r){
    { return (r - l); }; 
}

#endif
 ; 
void diff(float * v_initial_param_312_154, float * & v_user_func_315_155, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_315_155 = reinterpret_cast<float *>(malloc(((-1 + v_N_0) * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_152 = 0;(v_i_152 <= (-2 + v_N_0)); (++v_i_152)){
        // For each element reduced sequentially
        v_user_func_315_155[v_i_152] = 0.0f; 
        for (int v_i_153 = 0;(v_i_153 <= 1); (++v_i_153)){
            v_user_func_315_155[v_i_152] = diff2(v_user_func_315_155[v_i_152], v_initial_param_312_154[(v_i_152 + v_i_153)]); 
        }
    }
}
}; 