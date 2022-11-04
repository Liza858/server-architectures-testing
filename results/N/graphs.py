from matplotlib import pyplot as plt


def read_description():
    description = open("./description.txt")

    line = description.readline()[:-1].split(' ')
    test_param = line[1]
    line = description.readline()[:-1].split(' ')
    line = description.readline()[:-1].split(' ')
    array_size = int(line[1])
    line = description.readline()[:-1].split(' ')
    clients_count = int(line[1])
    line = description.readline()[:-1].split(' ')
    time_delta = int(line[1])
    line = description.readline()[:-1].split(' ')
    requests_count = int(line[1])
    line = description.readline()[:-1].split(' ')
    start_value = int(line[1])
    line = description.readline()[:-1].split(' ')
    end_value = int(line[1])
    line = description.readline()[:-1].split(' ')
    step = int(line[1])

    description.close()

    return (test_param, array_size, clients_count, time_delta,
            requests_count, start_value, end_value, step)


def create_test_param_array(start_value, end_value, step):
    array = []
    value = start_value
    while True:
        array.append(value)
        if value == end_value:
            break
        value = min(value + step, end_value)

    return array


def read_data(task_execution_data_file, client_process_data_file, request_average_data_file):
    f1 = open(task_execution_data_file)
    f2 = open(client_process_data_file)
    f3 = open(request_average_data_file)

    task_execution_data = list(map(float, f1.readlines()))
    client_process_data = list(map(float, f2.readlines()))
    request_average_data = list(map(float, f3.readlines()))

    f1.close()
    f2.close()
    f3.close()

    return (task_execution_data, client_process_data, request_average_data)


if __name__ == '__main__':
    test_param, array_size, clients_count, time_delta, \
        requests_count, start_value, end_value, step = read_description()
    test_param_array = create_test_param_array(start_value, end_value, step)

    print(read_description())

    blocking_task_execution, blocking_client_process, blocking_request_average = \
        read_data(
            "./blocking_taskExecutionTime.txt",
            "./blocking_clientProcessTime.txt",
            "./blocking_requestAverageTime.txt"
        )

    non_blocking_task_execution, non_blocking_client_process, non_blocking_request_average = \
        read_data(
            "./non_blocking_taskExecutionTime.txt",
            "./non_blocking_clientProcessTime.txt",
            "./non_blocking_requestAverageTime.txt"
        )

    asynchronous_task_execution, asynchronous_client_process, asynchronous_request_average = \
        read_data(
            "./asynchronous_taskExecutionTime.txt",
            "./asynchronous_clientProcessTime.txt",
            "./asynchronous_requestAverageTime.txt"
        )

    plt.plot(test_param_array, blocking_task_execution, color='r', label='blocking')
    plt.plot(test_param_array, non_blocking_task_execution, color='b', label='non-blocking')
    plt.plot(test_param_array, asynchronous_task_execution, color='g', label='asynchronous')
    plt.grid(True)
    plt.xlabel(test_param)
    plt.ylabel("time, ms")
    plt.title("task execution time")
    plt.legend()
    plt.show()

    plt.plot(test_param_array, blocking_client_process, color='r', label='blocking')
    plt.plot(test_param_array, non_blocking_client_process, color='b', label='non-blocking')
    plt.plot(test_param_array, asynchronous_client_process, color='g', label='asynchronous')
    plt.grid(True)
    plt.xlabel(test_param)
    plt.ylabel("time, ms")
    plt.title("client process time")
    plt.legend()
    plt.show()

    plt.plot(test_param_array, blocking_request_average, color='r', label='blocking')
    plt.plot(test_param_array, non_blocking_request_average, color='b', label='non-blocking')
    plt.plot(test_param_array, asynchronous_request_average, color='g', label='asynchronous')
    plt.grid(True)
    plt.xlabel(test_param)
    plt.ylabel("time, ms")
    plt.title("request average time")
    plt.legend()
    plt.show()
