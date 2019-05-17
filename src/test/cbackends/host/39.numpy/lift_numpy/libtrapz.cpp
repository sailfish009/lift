
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef TRAPZ_H
#define TRAPZ_H
; 
float trapz(float x1, float x2, float y1, float y2){
    { return (x2-x1)*(y2+y1)/2.0f; }; 
}

#endif
 ; 
#ifndef ADD_H
#define ADD_H
; 
float add(float l, float r){
    { return (l + r); }; 
}

#endif
 ; 
void trapz(float * v_initial_param_396_186, float * v_initial_param_397_187, float * & v_user_func_400_190, int v_N_0){
    // Allocate memory for output pointers
    float * v_user_func_424_189 = reinterpret_cast<float *>(malloc(((-1 + v_N_0) * sizeof(float))));
    v_user_func_400_190 = reinterpret_cast<float *>(malloc((1 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_185 = 0;(v_i_185 <= (-2 + v_N_0)); (++v_i_185)){
        v_user_func_424_189[v_i_185] = trapz(v_initial_param_396_186[v_i_185], v_initial_param_396_186[(1 + v_i_185)], v_initial_param_397_187[v_i_185], v_initial_param_397_187[(1 + v_i_185)]); 
    }
    // For each element reduced sequentially
    v_user_func_400_190[0] = 0.0f; 
    for (int v_i_184 = 0;(v_i_184 <= (-2 + v_N_0)); (++v_i_184)){
        v_user_func_400_190[0] = add(v_user_func_400_190[0], v_user_func_424_189[v_i_184]); 
    }
}
}; 