
## Measurement Interval Processing

### Assumptions and Edge Cases

##### To avoid spending time on requirements clarification —since this is not a real project — I made my own assumptions:

1. `startOfSampling` parameter is used to filter out samples that come before this value

2. Intervals are fixed, defined as a 5-minute spans and samples are projected onto this 5-minutes grid:
   > 10:02:01 Measurement Time<br>
   > 10:05:00 Next interval<br>

   > 10:05:00 Measurement Time<br>
   > 10:05:00 Next interval<br>

3. Theoretically there could be spans in measurements, some intervals could be missed  
   I simply consider it as a normal case and calculate next intervals for existing measurements:<br>

   > 10:05:01 Measurement Time<br>
   > 10:10:00 New Interval<br>
   > next measurement: <br>
   > 12:05:01 Measurement Time<br>
   > 12:10:00 New Interval<br>
4. Several measurements can have the exact same time as the upper bound of the interval they belong to. 
   In this case any of these measurements are relevant to be selected to the result

   > 10:05:00 Interval<br>
   > 10:05:00 Some measurement<br>
   > 10:05:00 One more measurement<br>

   any of them are relevant for selection
## Output Examples

> Input<br>
10:00:00 Start Time<br>
{2017-01-03T10:04:45, TEMP, 35.79}<br>
{2017-01-03T10:01:18, SPO2, 98.78}<br>
{2017-01-03T10:09:07, TEMP, 35.01}<br>
{2017-01-03T10:03:34, SPO2, 96.49}<br>
{2017-01-03T10:02:01, TEMP, 35.82}<br>
{2017-01-03T10:05:00, SPO2, 97.17}<br>
{2017-01-03T10:05:01, SPO2, 95.08}<br>
Output <br>
{2017-01-03T10:05:00, SPO2, 97.17}<br>
{2017-01-03T10:05:00, TEMP, 35.79}<br>
{2017-01-03T10:10:00, SPO2, 95.08}<br>
{2017-01-03T10:10:00, TEMP, 35.01}<br>

> Input<br>
10:05:00 Start Time<br>
{2017-01-03T10:04:45, TEMP, 35.79}<br>
{2017-01-03T10:01:18, SPO2, 98.78}<br>
{2017-01-03T10:09:07, TEMP, 35.01}<br>
{2017-01-03T10:03:34, SPO2, 96.49}<br>
{2017-01-03T10:02:01, TEMP, 35.82}<br>
{2017-01-03T10:05:00, SPO2, 97.17}<br>
{2017-01-03T10:05:01, SPO2, 95.08}<br>
Output <br>
{2017-01-03T10:05, SPO2, 97.17}<br>
{2017-01-03T10:10, SPO2, 95.08}<br>
{2017-01-03T10:10, TEMP, 35.01}<br>