import math
import os
from werkzeug.utils import secure_filename
from flask import Flask, request, Response, send_file
import requests
import json
import cv2
import logging
import tempfile
import threading
import shutil
import pdb

from ultralytics import YOLO

LAST_PHOTO = None
PHOTO_EVERY = float(5.0)
MANUAL_CUT_OFF = float(5.0)
STATUS_CHANGED = threading.Event()
PREV_MANUAL_STATUS = "closed"
MANUAL_STATUS = "closed"
_Homepath = '/home/pi/projekt/Animal/'
model = YOLO('/home/pi/projekt/best.onnx')
app = Flask(__name__)


@app.route('/manual/takePhoto')
def takePhoto():
    global LAST_PHOTO
    with tempfile.NamedTemporaryFile(delete=False, suffix='.jpg') as temp_file:
        cv2.imwrite(temp_file.name, LAST_PHOTO)

    return send_file(temp_file.name, as_attachment=True, download_name='photo.jpg')


@app.route('/manual/getPhotoEvery')
def getPhotoEvery():
    data = {"value": float(PHOTO_EVERY)}
    return Response(json.dumps(data), status=200)


@app.route('/manual/setPhotoEvery', methods=['POST'])
def setPhotoEvery():
    global PHOTO_EVERY
    PHOTO_EVERY = request.get_json()['value']
    return Response(status=200)


@app.route('/manual/connect')
def connect():
    data = {
        "connected": 'true'
    }
    return Response(json.dumps(data), status=200)


@app.route('/manual/getDoorCutOff')
def getDoorCutOff():
    data = {"value": float(MANUAL_CUT_OFF)}
    return Response(json.dumps(data), status=200)


@app.route('/manual/getDoorState')
def getDoorState():
    data = {"state": MANUAL_STATUS}
    return Response(json.dumps(data), status=200)


@app.route('/manual/setDoorCutOff', methods=['POST'])
def setDoor():
    global MANUAL_CUT_OFF
    MANUAL_CUT_OFF = request.get_json()['value']
    return Response(status=200)


@app.route('/manual/setActionDoor', methods=['POST'])
def openDoor():
    global STATUS_CHANGED
    global MANUAL_STATUS
    MANUAL_STATUS = request.get_json()['state']
    STATUS_CHANGED.set()
    return Response(status=200)


@app.route('/animal/addAnimal', methods=['POST'])
def addAnimal():
    animalPath = request.get_json()['animalName']
    path = _Homepath + animalPath
    if not os.path.exists(path):
        os.mkdir(path)

    return Response(status=200)


@app.route('/animal/popAnimal', methods=['POST'])
def popAnimal():
    animalPath = request.get_json()['animalName']
    path = _Homepath + animalPath
    print(path)
    # Jakby jakiś prankster chciał usunąć całą bazunie
    if path.endswith('Animal/'):
        return

    if os.path.exists(path):
        shutil.rmtree(path)

    return Response(status=200)


@app.route('/animal/addAnimalPhoto', methods=['POST'])
def addAnimalPhoto():
    animalName = request.form.get("animalName", '').strip('"')
    files = request.files.getlist("file")
    photoDir = _Homepath + animalName + '/'

    for file in files:
        filename = secure_filename(file.filename)
        filePath = photoDir + filename

        if os.path.exists(filePath):
            os.remove(filePath)
        file.save(filePath)

    return Response(status=200)


@app.route('/animal/getAnimalsList')
def getAnimals():
    data = []
    for animal in os.listdir(_Homepath):
        animalData = {
            "name": animal,
            "photos": []
        }
        for photo in os.listdir(_Homepath + animal):
            animalData['photos'].append(animal + '/' + photo)
        data.append(animalData)
    return json.dumps(data)


@app.route('/animal/getAnimalPhotos', methods=['POST'])
def getAnimalsPhotos():
    data = {'animalPhoto': []}
    aninalName = request.get_json()['animalName']
    for photo in os.listdir(_Homepath + request.get_json()['animalName']):
        data['animalPhoto'].append(aninalName + '/' + photo)
    return json.dumps(data)


@app.route('/animal/popAnimalPhoto', methods=['POST'])
def popAnimalPhoto():
    animalPhoto = request.get_json()['animalPhoto']

    path = _Homepath + animalPhoto
    if os.path.exists(path):
        os.remove(path)

    return Response(status=200)


@app.route('/animal/getPhoto')
def getPhoto():
    photo = request.args.get("path")
    animalPath = _Homepath + photo
    return send_file(animalPath)


@app.route('/animal/add/', methods=['POST'])
def check_cat():
    name = request.args.get("animalName")
    if os.path.isdir(_Homepath + name): return Response(status=208)
    os.mkdir(_Homepath + name)
    return Response(status=200)


@app.route('/si/updateModel/', methods=['POST'])
def updateModel():
    shutil.make_archive('animals', 'zip', _Homepath)

    files = {'zipFile': open('animals.zip', 'rb')}
    requests.post('http://172.24.171.29:5000/model/recieveData', files=files)

    os.remove('animals.zip')


CAMERA_OPEN = False


def background_camera():
    import time
    while True:
        global STATUS_CHANGED, LAST_PHOTO, CAMERA_OPEN, PHOTO_EVERY
        time.sleep(PHOTO_EVERY)
        cam = cv2.VideoCapture(0)
        _, frame = cam.read()
        cam.release()
        if frame is not None:
            results = model(frame, show_boxes=True, verbose=False)
            LAST_PHOTO = results[0].plot()
            for result in results:
                for box in result.boxes:
                    if math.isclose(box.cls[0], 0.0, abs_tol=0.01) or math.isclose(box.cls[0], 1.0, abs_tol=0.01):
                        print("Wykryto")
                        CAMERA_OPEN=True
                        STATUS_CHANGED.set()
                        break

                    if CAMERA_OPEN:
                        break
        else:
            print("Brak zdjęcia")

class background:

    def __init__(self):
        import RPi.GPIO as GPIO
        import time
        self.time = time
        self.GPIO = GPIO  # python jest dziwny
        self.in1 = 17
        self.in2 = 18
        self.in3 = 27
        self.in4 = 22

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.in1, GPIO.OUT)
        GPIO.setup(self.in2, GPIO.OUT)
        GPIO.setup(self.in3, GPIO.OUT)
        GPIO.setup(self.in4, GPIO.OUT)

        GPIO.setup(23, GPIO.IN)
        GPIO.output(self.in1, GPIO.LOW)
        GPIO.output(self.in2, GPIO.LOW)
        GPIO.output(self.in3, GPIO.LOW)
        GPIO.output(self.in4, GPIO.LOW)

        self.motor_pins = [self.in1, self.in2, self.in3, self.in4]
        self.motor_step_counter = 0
        self.step_count = 14000
        self.step_sequence = [[1, 0, 0, 0],
                              [1, 1, 0, 0],
                              [0, 1, 0, 0],
                              [0, 1, 1, 0],
                              [0, 0, 1, 0],
                              [0, 0, 1, 1],
                              [0, 0, 0, 1],
                              [1, 0, 0, 1]]

    def openclose(self, direction):
        GPIO = self.GPIO
        for _ in range(self.step_count):
            for pin in range(0, len(self.motor_pins)):
                GPIO.output(self.motor_pins[pin], self.step_sequence[self.motor_step_counter][pin])
            if direction:
                self.motor_step_counter = (self.motor_step_counter - 1) % 8
            elif not direction:
                self.motor_step_counter = (self.motor_step_counter + 1) % 8
            self.time.sleep(0.001)
        # reset pins
        GPIO.output(self.in1, GPIO.LOW)
        GPIO.output(self.in2, GPIO.LOW)
        GPIO.output(self.in3, GPIO.LOW)
        GPIO.output(self.in4, GPIO.LOW)

    # def openclose() end

    def background_opening(self):
        print("Weszlo do funkcji")
        time = self.time
        GPIO = self.GPIO
        timer = 0
        openclose = self.openclose
        while True:
            global STATUS_CHANGED, PREV_MANUAL_STATUS, MANUAL_STATUS, MANUAL_CUT_OFF, CAMERA_OPEN
            STATUS_CHANGED.wait()
            IR_STATUS = GPIO.input(23)
            if IR_STATUS == 0:  # brak reakcji jak cos blokuje
                print("blokuje")
                timer = 0
                time.sleep(1)
                # zeby nie zabilo kota
            # lock and unlock
            elif MANUAL_STATUS == 'unlocked':  # stan unlocked - drzwi poziomo na stale
                print("UNLOCKED")
                if PREV_MANUAL_STATUS != 'opened' and PREV_MANUAL_STATUS != 'unlocked':  # drzwi sa zamkniete (pionowo)
                    print("tutaj1")
                    openclose(True)
                print("tutaj111")
                PREV_MANUAL_STATUS = 'unlocked'  # zeby byl poprzedni stan odpowiedni
                STATUS_CHANGED.clear()  # stoppuje wykonywanie funkcji
                print("Odblokowano drzwi!")
            elif MANUAL_STATUS == 'locked':  # stan locked - drzwi pionowo na stale
                print("LOCKED")
                if PREV_MANUAL_STATUS != 'closed' and PREV_MANUAL_STATUS != 'locked':  # drzwi sa otwarte (poziomo)
                    print("tutaj2")
                    openclose(False)
                PREV_MANUAL_STATUS = 'locked'  # zmiana poprzedniego stanu na odpowieni
                STATUS_CHANGED.clear()  # stop funkcji
                print("Zablokowano drzwi!")
            elif CAMERA_OPEN:  # wykryto kota kamerka
                print("CAMERA ",MANUAL_STATUS)
                if PREV_MANUAL_STATUS != 'opened' and PREV_MANUAL_STATUS != 'unlocked':  # sprawdzenie poprzedniego stanu
                    print("tutaj3")
                    openclose(True)
                timer = time.time()  # wlaczenie timera
                CAMERA_OPEN = False  # zresetowanie stanu kamery
                PREV_MANUAL_STATUS = 'opened'  # zmiana poprzedniego stanu na opened (w przypadku innej funkcji)
                MANUAL_STATUS = 'toclose'  # zmiana stanu na oczekiwanie na zamkniecie
                print("Otworzono drzwi z kamerki!")
            elif MANUAL_STATUS == 'opened':  # jezeli stan to otworzenie
                print("OPENED")
                if PREV_MANUAL_STATUS != 'opened' and PREV_MANUAL_STATUS != 'unlocked':  # poprzedni stan
                    print("tutaj1")
                    openclose(True)
                timer = time.time()  # wlaczenie timera
                PREV_MANUAL_STATUS = 'opened'  # zmiana poprzedniego stanu na opened
                MANUAL_STATUS = 'toclose'  # zmiana stanu na oczekiwanie na zamkniecie
                print("Otworzono drzwi telefonem!")
            elif MANUAL_STATUS == 'toclose':  # oczekiwanie na zamkniecie
                if time.time() - timer > MANUAL_CUT_OFF:  # sprawdzenie czasu ktory uplynal
                    MANUAL_STATUS = 'closed'  # zmiana stanu na zamkniete
                    print("Czas minął!")
            elif MANUAL_STATUS == 'closed':  # zamkniete
                if PREV_MANUAL_STATUS != 'closed' and PREV_MANUAL_STATUS != 'locked':  # sprawdzenie poprzedniego stanu
                    print("tutaj55")
                    openclose(False)
                PREV_MANUAL_STATUS = 'closed'  # zmiana poprzedniego stanu
                STATUS_CHANGED.clear()  # zatrzymanie funkcji
                print("Zamknieto drzwi!")
    # def background_opening() end


def background_opening():
    print("Uruchomiony wątek")
    bg = background()
    print("Utworzona klasa")
    bg.background_opening()


if __name__ == '__main__':
    logFile = './log/actionsLog.log'
    if not os.path.exists(os.path.dirname(logFile)):
        os.makedirs(os.path.dirname(logFile))
    logging.basicConfig(filename=logFile, encoding='utf-8', level=logging.INFO,format='%(asctime)s %(message)s', datefmt='%m/%d/%Y %I:%M:%S %p')
    logging.getLogger().setLevel(logging.ERROR)
    # i to musi byc na threadach bo flask uruchamia tylko te funkcje do ktorych dostanie request
    camera=threading.Thread(target=background_camera)
    camera.start()
    open = threading.Thread(target=background_opening)
    open.start()
    app.run(debug=False, host='0.0.0.0', port=5000)
