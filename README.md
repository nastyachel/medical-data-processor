
## Measurement Interval Processing

### Assumptions and Edge Cases

##### To avoid spending time on requirements clarification —since this is not a real project — I made my own assumptions:

1. `startOfSampling` parameter can be < any of the measuring time in measures  
   Simply calculate a new start based on the oldest measurement for the corresponding type:<br>

   > 10:05:00 Start Time<br>
   > 10:05:01 Measurement Time<br>
   > 10:10:00 New Start Time<br>

2. Intervals are calculated just by adding 5 minutes, starting from the start time:<br>
If start of sampling is "10:02:01", the next interval is "10:07:01"
   > 10:02:01 Start Time<br>
   > 10:07:01 Next interval<br>

3. Theoretically there could be spans in measurements, some intervals could be missed  
   I simply consider it as a normal case and calculate next intervals for existing measurements:<br>

   > 10:05:00 Start Time<br>
   > 12:05:01 Measurement Time<br>
   > 12:10:00 New Interval<br>

4. Several measurements have the exact same time as the interval they belong to  
   In this case any of these measurements are relevant to be selected to the result

   > 10:05:00 Interval<br>
   > 10:05:00 Some measurement<br>
   > 10:05:00 One more measurement<br>

   any of them are relevant for selection
## Output Examples

> Input<br>
10:05:00 Start Time<br>
{2017-01-03T10:05, SPO2, 97.17}<br>
{2017-01-03T10:05, TEMP, 35.79}<br>
{2017-01-03T10:10, SPO2, 95.08}<br>
{2017-01-03T10:10, TEMP, 35.01}<br>
Output <br>
{2017-01-03T10:05, SPO2, 97.17}<br>
{2017-01-03T10:05, TEMP, 35.79}<br>
{2017-01-03T10:10, SPO2, 95.08}<br>
{2017-01-03T10:10, TEMP, 35.01}<br>

> Input<br>
10:00:00 Start Time<br>
{2017-01-03T10:05, SPO2, 97.17}<br>
{2017-01-03T10:05, TEMP, 35.79}<br>
{2017-01-03T10:10, SPO2, 95.08}<br>
{2017-01-03T10:10, TEMP, 35.01}<br>
Output <br>
{2017-01-03T10:05, SPO2, 97.17}<br>
{2017-01-03T10:05, TEMP, 35.79}<br>
{2017-01-03T10:10, SPO2, 95.08}<br>
{2017-01-03T10:10, TEMP, 35.01}<br>