FROM python:3.9.18

WORKDIR /usr/app/src

COPY News_Category_Dataset_v3.json ./News_Category_Dataset_v3.json
COPY requirements.txt ./requirements.txt
COPY json_to_csv.py ./json_to_csv.py

RUN pip install -r requirements.txt

CMD ["python", "./json_to_csv.py"]