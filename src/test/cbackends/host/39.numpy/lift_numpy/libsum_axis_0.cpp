
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
void sum_axis_0(float * v_initial_param_3318_528, float * & v_user_func_3321_529, int v_N_352, int v_M_353){
    // Allocate memory for output pointers
    v_user_func_3321_529 = reinterpret_cast<float *>(malloc((v_N_352 * sizeof(float)))); 
    {
        
    }
    // For each element processed sequentially
    for (int v_i_526 = 0;(v_i_526 <= (-1 + v_N_352)); (++v_i_526)){
        // For each element reduced sequentially
        v_user_func_3321_529[v_i_526] = 0.0f; 
        for (int v_i_527 = 0;(v_i_527 <= (-1 + v_M_353)); (++v_i_527)){
            v_user_func_3321_529[v_i_526] = add(v_user_func_3321_529[v_i_526], v_initial_param_3318_528[(v_i_526 + (v_N_352 * v_i_527))]); 
        }
    }
}
}; 