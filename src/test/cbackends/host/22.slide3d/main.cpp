#include <bits/stdc++.h>
#include "./libslide3d.cpp"

using namespace std;

int main(int argc, char *argv[])
{

	int const N = 4;
	int const output_N = 2;
	vector<float> in(N*N*N,1.0f);
	float *out = nullptr;

        /* std::cout << "output size = " << (-8 + (-6 * (int)pow((float)N, 2)) + (12 * N) + (int)pow((float)N, 3)) << std::endl; */

	execute(in.data(), out, N);

	copy(out,out+output_N*output_N*output_N, ostream_iterator<float>(cout, " "));
	std::cout << std::endl;
	
	return 0;
}
