import sqlite3
import mysql.connector
import os
import datetime

# Configuration
SQLITE_DB_PATH = '/Users/meijin/Documents/THAI电商工作流项目/exchange/deploy/jenkins+nuexs/AImanju/backend-java/data/drama.db'
MYSQL_CONFIG = {
    'user': 'root',
    'password': '',
    'host': 'localhost',
    'database': 'drama_db',
    'raise_on_warnings': True
}

def convert_timestamp(val):
    """Convert potential millisecond timestamp to MySQL datetime format"""
    if val is None:
        return None
    # Check if it looks like a millisecond timestamp (13 digits)
    if isinstance(val, (int, float, str)):
        try:
            # Try to parse as float/int
            ts = float(val)
            # If > 10^11, it's likely milliseconds (e.g. 1771933298707)
            # Standard unix timestamp is ~1.7 * 10^9 (10 digits)
            if ts > 100000000000: 
                ts = ts / 1000.0
            
            # Convert to YYYY-MM-DD HH:MM:SS format
            return datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        except (ValueError, OSError, OverflowError):
            pass
    return val

def migrate():
    # ... (connection setup) ...
    if not os.path.exists(SQLITE_DB_PATH):
        print(f"Error: SQLite database not found at {SQLITE_DB_PATH}")
        return

    print("Connecting to databases...")
    sqlite_conn = sqlite3.connect(SQLITE_DB_PATH)
    # Use Row factory to access columns by name
    sqlite_conn.row_factory = sqlite3.Row
    sqlite_cursor = sqlite_conn.cursor()

    try:
        mysql_conn = mysql.connector.connect(**MYSQL_CONFIG)
        mysql_cursor = mysql_conn.cursor()
    except mysql.connector.Error as err:
        print(f"Error connecting to MySQL: {err}")
        return

    # Get list of tables from SQLite
    sqlite_cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row['name'] for row in sqlite_cursor.fetchall() if row['name'] != 'sqlite_sequence']

    # Disable foreign key checks in MySQL temporarily
    mysql_cursor.execute("SET FOREIGN_KEY_CHECKS=0;")

    for table in tables:
        print(f"Migrating table: {table}")
        
        # Get data from SQLite
        try:
            sqlite_cursor.execute(f"SELECT * FROM {table}")
            rows = sqlite_cursor.fetchall()
        except sqlite3.OperationalError as e:
             print(f"Skipping table {table} due to error: {e}")
             continue

        if not rows:
            print(f"  - No data in {table}")
            continue

        # Get columns
        columns = rows[0].keys()
        col_names = ", ".join([f"`{col}`" for col in columns])
        placeholders = ", ".join(["%s"] * len(columns))
        
        # Prepare INSERT statement
        insert_query = f"INSERT INTO `{table}` ({col_names}) VALUES ({placeholders})"
        
        # Insert data into MySQL
        data_to_insert = []
        for row in rows:
            row_data = []
            for col_name in columns:
                val = row[col_name]
                # Check if column name suggests a timestamp
                if col_name in ['created_at', 'updated_at', 'deleted_at', 'completed_at', 'time']:
                     val = convert_timestamp(val)
                row_data.append(val)
            data_to_insert.append(tuple(row_data))

        try:
            # Clear existing data in MySQL table
            mysql_cursor.execute(f"DELETE FROM `{table}`")
            
            mysql_cursor.executemany(insert_query, data_to_insert)
            mysql_conn.commit()
            print(f"  - Migrated {len(data_to_insert)} rows.")
        except mysql.connector.Error as err:
            print(f"  - Error migrating table {table}: {err}")

    # Re-enable foreign key checks
    mysql_cursor.execute("SET FOREIGN_KEY_CHECKS=1;")
    
    sqlite_conn.close()
    mysql_conn.close()
    print("Migration completed.")

if __name__ == "__main__":
    migrate()
