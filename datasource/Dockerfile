FROM python:3.9.18

WORKDIR /usr/app/src

COPY requirements.txt ./requirements.txt
RUN pip install -r requirements.txt

COPY json_to_csv.py ./json_to_csv.py
CMD ["python", "./json_to_csv.py"]

COPY News_Category_Dataset_v3.json ./News_Category_Dataset_v3.json