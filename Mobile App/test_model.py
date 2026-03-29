import numpy as np
import tensorflow as tf
import os

model_path = os.path.join(os.path.dirname(__file__), 'app', 'src', 'main', 'assets', 'skin_health_model.tflite')

interpreter = tf.lite.Interpreter(model_path=model_path)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()[0]
output_details = interpreter.get_output_details()[0]

print("Input details:", input_details['shape'], input_details['dtype'])
print("Output details:", output_details['shape'], output_details['dtype'])

# Try predicting with all zeros, all ones, and random data
for name, data in [
    ("Zeros (RGB 0)", np.zeros(input_details['shape'], dtype=np.float32)),
    ("Ones (RGB 1)", np.ones(input_details['shape'], dtype=np.float32)),
    ("Random [0,1]", np.random.rand(*input_details['shape']).astype(np.float32)),
    ("Random [-1,1]", (np.random.rand(*input_details['shape']) * 2 - 1).astype(np.float32)),
    ("Random [0,255]", (np.random.rand(*input_details['shape']) * 255).astype(np.float32)),
]:
    interpreter.set_tensor(input_details['index'], data)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details['index'])[0]
    print(f"{name:15}: {output} -> Max index {np.argmax(output)} ({output[np.argmax(output)]:.2f})")
