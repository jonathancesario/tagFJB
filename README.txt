HOT TAG FJB KASKUS PREDICTION

Preparations
1. Install all Maven project dependencies
2. Run hsqldb database using this command below (execute on /lib)
	java -cp hsqldb.jar org.hsqldb.Server -database.0 file:trendFJB -dbname.0 xdb
3. Put data on \src\main\resources\datasource\data using yyyy-MM-dd.csv format
3. Run the server (Main.java)
4. Open localhost:4567(default) from browser

Notes
1. You can see the comparison of every prediction on \src\main\resources\datasource\summary
2. You must do initialization if there isn't any history data yet on Initialize tab
3. Get New Day will predict for one day (data for next day must be exist)