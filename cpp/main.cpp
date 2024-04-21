#include <iostream>
#include <limits>
#include <omp.h>
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

int main() {
  std::vector<std::vector<int>> data = {
      {1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}};

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
