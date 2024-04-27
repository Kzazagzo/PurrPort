import math
import os
from pathlib import Path

import numpy as np
from werkzeug.utils import secure_filename
from flask import Flask, request, Response, send_file
import json
import cv2
from ultralytics import YOLO
import tempfile
import shutil
import yaml



#boxes = model(buffer).boxes - to rysuje boxy... do tego trzeba jeszcze keypoints i z
#                                           tego robisz nowy model
#probs = model(buffer).probs - sprawdza co było na obrazku - jak wykryje naszego kota
#                                                           odpalasz drzwi


app = Flask(__name__)

@app.route('/model',methods=['POST'])
def createModel():
    name = request.args.get("animalName")
    model = YOLO('yolov8n.pt')

    return Response(status=200)

def generateBoundings():
    path = os.path.dirname(os.path.abspath(__file__))
    model = YOLO(f'{path}/boxGenerator.pt')
    nc = len(next(os.walk(f'{path}/Animals/'))[1])

    animalNames = []
    for i,animal in enumerate(os.listdir(f'{path}/Animals/')):
        animalNames.append(animal)
        for photo in os.listdir(f'{path}/Animals/{animal}'):
            photoPath = f'{path}/Animals/{animal}/{photo}'
            results = model(photoPath)
            Path('./Model/labels').mkdir(parents=True, exist_ok=True)
            labelName = photo.replace('.jpg','.txt')
            labelPath = f'./Model/labels/{labelName}'

            lines = 0
            with open(labelPath,'w') as f:
                for result in results:
                    for box in result.boxes:
                        if math.isclose(box.cls[0], 16.0, abs_tol=0.01) or math.isclose(box.cls[0], 15.0, abs_tol=0.01):
                            for (x, y, w, h) in (np.array(box.xywhn.cpu())):
                                f.write(f'{i} {x} {y} {w} {h}\n')
                                lines += 1
            if lines == 0:
                os.remove(labelPath)
            else:
                Path('./Model/images').mkdir(parents=True, exist_ok=True)
                imagePath = f'./Model/images/{photo}'
                shutil.copy(photoPath, imagePath)

    data = {
        'train': './Model/train/images/',
        'nc': nc,
        'names': animalNames
    }
    with open('data.yaml', 'w') as f:
        yaml.dump(data, f)

def detect():
    path = os.path.dirname(os.path.abspath(__file__))
    model = YOLO(f'{path}/best.pt')
    results = model(f'{path}/IMG20240113214749.jpg', save=True)

    # Visualize the results on the frame


@app.route('/model/recieveData',methods=['POST'])
def recieveData():
    f = request.files['file']
    _, temp_zip_path = tempfile.mkstemp(suffix='.zip')

    f.save(temp_zip_path)

    extract_path = '.'
    shutil.unpack_archive(temp_zip_path, extract_path)

    os.remove(temp_zip_path)

    return Response(status=200)


if __name__ == '__main__':
    detect()
   # app.run(debug=True, host='0.0.0.0', port=5000)
