#include <iostream>
#include <limits>
#include <omp.h>
#include <random>
#include <vector>

int computeTotalSum(const std::vector<std::vector<int>> &array) {
  int totalSum = 0;
#pragma omp parallel for reduction(+ : totalSum)
  for (int i = 0; i < array.size(); i++) {
    for (int j = 0; j < array[i].size(); j++) {
      totalSum += array[i][j];
    }
  }
  return totalSum;
}

std::pair<int, int> findMinRowSum(const std::vector<std::vector<int>> &array) {
  int minSum = std::numeric_limits<int>::max();
  int minIndex = -1;
#pragma omp parallel for
  for (int i = 0; i < array.size(); i++) {
    int rowSum = 0;
    for (int j = 0; j < array[i].size(); j++) {
      rowSum += array[i][j];
    }
#pragma omp critical
    {
      if (rowSum < minSum) {
        minSum = rowSum;
        minIndex = i;
      }
    }
  }
  return {minIndex, minSum};
}

std::vector<std::vector<int>> generateRandomData(int rows, int cols) {
  std::vector<std::vector<int>> data(rows, std::vector<int>(cols));
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> distrib(-1000, 1000);

  for (auto &row : data) {
    for (auto &elem : row) {
      elem = distrib(gen);
    }
  }
  return data;
}

int main() {
  int numRows = 1000;
  int numCols = 1000;

  std::vector<std::vector<int>> data = generateRandomData(numRows, numCols);

  int totalSum;
  std::pair<int, int> minRowSum;

  double start_time = omp_get_wtime();

#pragma omp parallel sections
  {
#pragma omp section
    { totalSum = computeTotalSum(data); }
#pragma omp section
    { minRowSum = findMinRowSum(data); }
  }

  double time_elapsed = omp_get_wtime() - start_time;

  std::cout << "Total Sum: " << totalSum << std::endl;
  std::cout << "Row with minimum sum is: " << minRowSum.first + 1
            << " with a sum of: " << minRowSum.second << std::endl;
  std::cout << "Time elapsed: " << time_elapsed << " seconds." << std::endl;

  return 0;
}

