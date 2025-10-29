package main

// 1. Crie uma função exec que dorme por um tempo e em seguida
// retorna o valor numérico do intervalo de tempo o qual dormiu.
// A função deve receber um parâmetro que é o tempo máximo que
// pode dormir (em ms);

import (
	"time"
	"fmt"
)


func exec(sleep int) int {
	time.Sleep(time.Duration(sleep) * time.Millisecond)
	return sleep
}

func auxiliar(max_sleep_ms int) chan int {
	numbers := make(chan int, 1_000)

	go func() {
		for i := 0; i < 1_000; i++ {
			numbers <- exec(max_sleep_ms)
		}
		close(numbers)
	}()

	return numbers
}

func main() {
	numbers_1 := auxiliar(20)
	numbers_2 := auxiliar(200)

	sum := 0

	for i := 0; i < 1_000; i++ {
		select {
			case x := <- numbers_1:
				sum += x
			case y := <- numbers_2:
				sum += y
		}
	}

	fmt.Println(sum)
}

