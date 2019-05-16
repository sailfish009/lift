
#include <bits/stdc++.h>

using namespace std;

    
namespace lift {; 
#ifndef HYPOT_UF_H
#define HYPOT_UF_H
; 
float hypot_uf(float x, float y){
    { return sqrt((x*x)+(y*y)); }; 
}

#endif
 ; 
void hypot(float * v_initial_param_138_77, float * v_initial_param_139_78, float * & v_user_func_145_80, int v_N_0){
    // Allocate memory for output pointers
    v_user_func_145_80 = reinterpret_cast<float *>(malloc((v_N_0 * sizeof(float)))); 
    // For each element processed sequentially
    for (int v_i_76 = 0;(v_i_76 <= (-1 + v_N_0)); (++v_i_76)){
        v_user_func_145_80[v_i_76] = hypot_uf(v_initial_param_138_77[v_i_76], v_initial_param_139_78[v_i_76]); 
    }
}
}; 