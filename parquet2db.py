import pandas as pd
from sqlalchemy import create_engine

# 1. 读取 parquet（把路径换成你的）
df = pd.read_parquet("/Users/lxn/Downloads/pharmcube2harbour_ci_tracking_info_0.parquet")

# 2. 连接 PostgreSQL（修改这里！）
# 格式：postgresql+psycopg2://用户名:密码@主机:端口/库名
engine = create_engine(
    "postgresql+psycopg2://postgres:你的密码@localhost:5432/你的库名",
    connect_args={'options': '-c client_encoding=utf8'}
)

# 3. 直接导入！自动建表，不会乱码，不会报错
df.to_sql(
    name="harbour_ci_tracking_info",  # 表名
    con=engine,
    if_exists="replace",  # 存在就替换（replace/append）
    index=False,
    chunksize=1000
)

print("✅ Parquet 直接导入 PostgreSQL 成功！")