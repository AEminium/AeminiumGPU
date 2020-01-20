#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define START_LENGTH 128
#define MAX_BLOCK_SIZE 64

int predicate(int value) {
    if( (value > 7) && (value % 2) && !(value % 3) ) {
        return 1;
    } else {
        return 0;
    }
}

int* check_array(int *array, int length) {
    int *array_out = (int*)malloc(sizeof(int) * length);
    for(int i=0; i<length; i++) {
        array_out[i] = predicate(array[i]);
    }
    return array_out;
}

int* prefix_sum(int *array, int length) {
    int *array_out = (int*)malloc(sizeof(int) * length);
    array_out[0] = array[0];
    for(int i=1; i<length; i++) {
        array_out[i] = array[i] + array_out[i-1];
    }
    return array_out;
}

int* sieve_out(int *values, int *booleans, int *indexes, int *length) {
    int new_length = 0;
    for(int i=0; i<*length; i++) {
        if(booleans[i]) {
            new_length++;
        }
    }

    int *array_out = (int*)malloc(sizeof(int) * new_length);
    for(int i=0; i<*length; i++) {
        if(booleans[i]) {
            array_out[indexes[i]-1] = values[i];
        }
    }

    *length = new_length;
    return array_out;
}

void print_array(int *array, int length) {
    int *ptr = array;
    int counter = 0;
    while(ptr < array+length) {
        printf("array[%d] = %d\n", counter, *ptr);
        counter++;
        ptr++;
    }
}

int* seq_filter(int *array, int *length) {
    int *checked = check_array(array, *length);
    int *aggregated = prefix_sum(checked, *length);
    int *filtered = sieve_out(array, checked, aggregated, length);

    return filtered;
}

// #pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable

__global__ void filterKernel(int *values, int *bools, int *prefix_sum, int *output, int *array_length, int *semaphore) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;  // <---------<< 
    int covered_id = *array_length-1 - 2*idx;
    int threads = *array_length / 2;
    int blocks = threads / MAX_BLOCK_SIZE + (threads % MAX_BLOCK_SIZE > 0);
    int modulo = 2;

    // printf("Idx: %d, blockIdx.x: %d, blockDim.x: %d, threadIdx.x: %d\n", idx, blockIdx.x, blockDim.x, threadIdx.x);

    if((values[covered_id] > 7) && (values[covered_id] % 2) && !(values[covered_id] % 3)) {
        bools[covered_id] = 1;
        prefix_sum[covered_id] = 1;
    } else {
        bools[covered_id] = 0;
        prefix_sum[covered_id] = 0;
    }

    if(covered_id-1 >= 0) {
        if((values[covered_id-1] > 7) && (values[covered_id-1] % 2) && !(values[covered_id-1] % 3)) {
            bools[covered_id-1] = 1;
            prefix_sum[covered_id-1] = 1;
        } else {
            bools[covered_id-1] = 0;
            prefix_sum[covered_id-1] = 0;
        }
    }

    // //synchronizing before prefix_sum;
    __syncthreads();  // <---------<< 
    // opencl --> int atomic_inc(volatile __global *int)
    // if(threads > MAX_BLOCK_SIZE) {
    //     if(threadIdx.x == 0)
    //         atomicInc((unsigned int*)semaphore, *array_length);
    //     while(*semaphore != blocks) {}
    //     if(idx == 0)
    //         atomicXor(semaphore, *semaphore);
    // }

    // counting the prefix sum
    while(modulo < (2**array_length)) {
        // int active_threads = (threads + (modulo/2 -1)) / (modulo/2);
        if((covered_id % modulo) == ((*array_length-1) % modulo)) {
            if((covered_id - modulo/2) >= 0) {
                prefix_sum[covered_id] += prefix_sum[covered_id-modulo/2];
            }

            // sync in between levels of summing
            __syncthreads();  // <---------<< 
        }
        modulo *= 2;
    }

    if(covered_id == *array_length-1) {
        prefix_sum[covered_id] = 0;
    }

    // sync after up-sweep
    __syncthreads();  // <---------<< 

    while(modulo >= 2) {
        if((covered_id % modulo) == ((*array_length-1) % modulo)) {
            if((covered_id - modulo/2) >= 0) {
                int temp = prefix_sum[covered_id];
                prefix_sum[covered_id] += prefix_sum[covered_id - modulo/2];
                prefix_sum[covered_id - modulo/2] = temp;
            }
        }

        __syncthreads();  // <---------<< 
        __syncthreads();  // <---------<< 
        modulo /= 2;
    }

    if(bools[covered_id])
        prefix_sum[covered_id] += 1;
    if((covered_id-1) >= 0)
        if(bools[covered_id-1])
            prefix_sum[covered_id-1] += 1;

    __syncthreads();  // <---------<< 

    if(bools[covered_id]) {
        output[prefix_sum[covered_id]-1] = values[covered_id];
    }
    if(((covered_id - 1) >= 0) && (bools[covered_id-1])) {
        output[prefix_sum[covered_id-1]-1] = values[covered_id-1];
    }

    if(idx == 0) {
        *array_length = prefix_sum[*array_length-1];
    }

}

int* gpu_filter(int* array, int *length) {
    int *values, *bools, *prefix_sum, *output, *array_length, *semaphore;
    int size = sizeof(int) * *length;
    cudaMalloc(&values, size);  // <---------<< 
    cudaMalloc(&bools, size);  // <---------<< 
    cudaMalloc(&prefix_sum, size);  // <---------<< 
    cudaMalloc(&output, size);  // <---------<< 
    cudaMalloc(&array_length, sizeof(int));  // <---------<< 
    cudaMalloc(&semaphore, sizeof(int)*2);  // <---------<< 
    cudaMemcpy(values, array, size, cudaMemcpyHostToDevice);  // <---------<< 
    cudaMemcpy(array_length, length, sizeof(int), cudaMemcpyHostToDevice);  // <---------<< 

    
    int threads = *length / 2;
    int blocks = threads / MAX_BLOCK_SIZE + (threads % MAX_BLOCK_SIZE > 0);
    int threads_in_block = (threads + blocks-1) / blocks;

    filterKernel<<<blocks, threads_in_block>>>(values, bools, prefix_sum, output, array_length, semaphore);  // <---------<< 

    cudaMemcpy(length, array_length, sizeof(int), cudaMemcpyDeviceToHost);  // <---------<< 
    int new_size = *length * sizeof(int);  // <---------<< 
    cudaMemcpy(array, output, new_size, cudaMemcpyDeviceToHost);  // <---------<< 
    cudaFree(&values);  // <---------<< 
    cudaFree(&bools);  // <---------<< 
    cudaFree(&prefix_sum);  // <---------<< 
    cudaFree(&output);  // <---------<< 
    cudaFree(&array_length);  // <---------<< 
    cudaFree(&semaphore);  // <---------<< 

    return array;
}

int main() {
    srand(time(NULL));
    int length = START_LENGTH;

    int *array = (int*)malloc(sizeof(int) * length);
    for(int i=0, *ptr=array; i<length; i++, ptr++) {
        *ptr = rand()%length;
    }

    int* filtered = seq_filter(array, &length);
    print_array(filtered, length);

    printf("\n-------------------- array[0] = %d --------------------\n\n", array[0]);

    length = START_LENGTH;
    int* gpu_filtered = gpu_filter(array, &length);
    print_array(gpu_filtered, length);

    bool coherent = true;
    int iter = 0;
    while(coherent && (iter < length)) {
        if(filtered[iter] != array[iter]) {
            coherent = false;
        }
        iter++;
    }
    if(coherent)
        printf("\x1b[1;40;32mCorrect :)\x1b[0m\n");
    else
        printf("\x1b[1;40;31mIncorrect!\x1b[0m\n");
}