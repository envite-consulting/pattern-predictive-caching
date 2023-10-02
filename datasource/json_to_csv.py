import pandas as pd
news = pd.read_json('./News_Category_Dataset_v3.json', lines=True)
news['link'] = news['link'].str.replace('https://www.huffingtonpost.comhttp', 'http')
news = news.drop_duplicates(subset=['link'])
news.to_csv('./News_Category_Dataset_v3.csv', index=False)