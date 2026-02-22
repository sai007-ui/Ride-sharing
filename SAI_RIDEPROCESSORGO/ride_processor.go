package main

import (
	"fmt"
	"log"
	"os"
	"sync"
	"time"
)

type TaskQueue struct {
	tasks []string
	lock  sync.Mutex
}

func (q *TaskQueue) GetTask() (string, bool) {
	q.lock.Lock()
	defer q.lock.Unlock()
	if len(q.tasks) == 0 {
		return "", false
	}
	task := q.tasks[0]
	q.tasks = q.tasks[1:]
	return task, true
}

func main() {
	logFile, err := os.OpenFile("ride_processor.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
	if err != nil {
		log.Fatalf("Log file error: %v", err)
	}
	defer logFile.Close()
	log.SetOutput(logFile)

	queue := &TaskQueue{tasks: make([]string, 0)}
	for i := 1; i <= 12; i++ {
		queue.tasks = append(queue.tasks, fmt.Sprintf("RideRequest-%d", i))
	}

	var results []string
	var resultLock sync.Mutex
	var wg sync.WaitGroup
	numWorkers := 3

	for i := 0; i < numWorkers; i++ {
		wg.Add(1)
		go processWorker(i, queue, &results, &resultLock, &wg)
	}

	wg.Wait()

	fmt.Println("\nFinal Results:")
	for _, r := range results {
		fmt.Println(r)
	}
}

func processWorker(id int, queue *TaskQueue, results *[]string, lock *sync.Mutex, wg *sync.WaitGroup) {
	defer wg.Done()
	log.Printf("Worker %d started.", id)

	for {
		task, ok := queue.GetTask()
		if !ok {
			break
		}

		time.Sleep(400 * time.Millisecond)
		result := fmt.Sprintf("Worker %d completed %s", id, task)

		lock.Lock()
		*results = append(*results, result)
		lock.Unlock()

		log.Println(result)
	}

	log.Printf("Worker %d finished.", id)
}
