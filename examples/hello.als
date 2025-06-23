// hello.als
// A minimal "Hello World" Alloy model

sig Hello {}

pred showHello {
  some Hello
}

run showHello for 1
