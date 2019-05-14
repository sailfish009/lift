#include <bits/stdc++.h>

#include <lift_numpy.hpp>

using namespace std;

int main(int argc, char *argv[])
{

	vector<float> x {0,30,60,90,120,150,180,210,240,270,300,330};
	float *y = nullptr;
	const int size = x.size();

	lift::radians(x.data(),y,size);
	/* lift::cos(x.data(),y,5); */

	copy(y, y+size, ostream_iterator<float>(cout, " "));
	std::cout << std::endl;

	system("mkdir -p numpy_golden_data");
	system("./numpy/radians.py > numpy_golden_data/radians.txt");


	const int golden_data_size = size;
	vector<float> golden_data(golden_data_size, -999.99);

	ifstream file("numpy_golden_data/radians.txt");

	if (!file.good()) {
		fprintf(stderr, "Could not open the data file.\n");
		return( 1 );
	}

	istream_iterator<float> start(file), end;
	copy(start, end, golden_data.begin());

	const float tol = 0.0001;

	for(int i = 0; i < golden_data_size; ++i)
	{
		/* std::cout << abs( golden_data[i] - y[i] ) << std::endl; */
		if( abs( golden_data[i] - y[i] ) > tol ){
			std::cout << "[radians]: Computed results does not match the golden data !!!" << std::endl;
			exit(1);
		}
	}



	return 0;
}