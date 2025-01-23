# Measurement Interval Processing

## Assumptions and Edge Cases

#### To avoid spending time on requirements clarification — since this is not a real project — I made my own assumptions:

1. **`startOfSampling` parameter** is used to filter out samples that come before this value.

2. Intervals are fixed, defined as 5-minute spans, and samples are projected onto this 5-minute grid:
   - **Example**:
     ```
     10:02:01 Measurement Time
     10:05:00 Next interval

     10:05:00 Measurement Time
     10:05:00 Next interval
     ```

3. Theoretically, there could be gaps in measurements, meaning some intervals could be missed.  
   This is considered normal, and the next intervals are calculated based on the existing measurements:
   - **Example**:
     ```
     10:05:01 Measurement Time
     10:10:00 Next Interval
     
     Next Measurement:
     12:05:01 Measurement Time
     12:10:00 Next Interval
     ```

4. Several measurements can have the exact same time as the interval they belong to.  
   In this case, any of these measurements are relevant to be selected in the result.
   - **Example**:
     ```
     10:05:00 Interval
     10:05:00 Some Measurement
     10:05:00 One More Measurement
     ```
     Any of them are relevant for selection.

## Examples


- **Example 1**:
```
    10:00:00 Start Time
    Input
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:01:18, SPO2, 98.78}
    {2017-01-03T10:09:07, TEMP, 35.01}
    {2017-01-03T10:03:34, SPO2, 96.49}
    {2017-01-03T10:02:01, TEMP, 35.82}
    {2017-01-03T10:05:00, SPO2, 97.17}
    {2017-01-03T10:05:01, SPO2, 95.08}
    Output
    {2017-01-03T10:05:00, SPO2, 97.17}
    {2017-01-03T10:05:00, TEMP, 35.79}
    {2017-01-03T10:10:00, SPO2, 95.08}
    {2017-01-03T10:10:00, TEMP, 35.01}
```
- **Example 2**:
```
    10:05:00 Start Time
    Input
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:04:45, TEMP, 35.79}
    {2017-01-03T10:01:18, SPO2, 98.78}
    {2017-01-03T10:09:07, TEMP, 35.01}
    {2017-01-03T10:03:34, SPO2, 96.49}
    {2017-01-03T10:02:01, TEMP, 35.82}
    {2017-01-03T10:05:00, SPO2, 97.17}
    {2017-01-03T10:05:01, SPO2, 95.08}
    Output (lass values due to filtering out by start date)
    {2017-01-03T10:05:00, SPO2, 97.17}
    {2017-01-03T10:10:00, SPO2, 95.08}
    {2017-01-03T10:10:00, TEMP, 35.01}
```
