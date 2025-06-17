# Test Scenarios for OrderExpirationTaskRepositoryTest

This document outlines the test scenarios for the `OrderExpirationTaskRepositoryTest` class, which verifies the functionality of the `OrderExpirationTaskRepository` in a Spring Data JPA environment. The tests focus on the `findByStatusAndExpirationTimeBefore` method, ensuring it correctly retrieves `OrderExpirationTask` entities based on their status and expiration time.

## Test Scenario 1: Retrieve Matching Tasks by Status and Expiration Time

**Purpose**:  
Verify that `findByStatusAndExpirationTimeBefore` returns only `OrderExpirationTask` entities with a `PENDING` status and an expiration time before the specified timestamp.

**Preconditions**:  
- The database is empty.
- A timestamp (`now`) is set to the current time.
- Four `Order` entities with `CREATED` status are persisted.
- Four `OrderExpirationTask` entities are created:
  - Task 1: `PENDING` status, expiration time 1 hour before `now`.
  - Task 2: `PENDING` status, expiration time 2 hours before `now`.
  - Task 3: `PENDING` status, expiration time 1 hour after `now`.
  - Task 4: `SENT` status, expiration time 1 hour before `now`.

**Steps**:  
1. Persist four `Order` entities.
2. Persist four `OrderExpirationTask` entities with the specified statuses and expiration times.
3. Flush the entity manager to ensure data is written to the database.
4. Call `findByStatusAndExpirationTimeBefore(TaskStatus.PENDING, now)` to retrieve tasks.
5. Verify the results.

**Expected Outcome**:  
- The result list contains exactly 2 tasks (Task 1 and Task 2).
- The tasks in the result have order IDs matching those of Task 1 and Task 2.
- All tasks in the result have `PENDING` status.

## Test Scenario 2: Return Empty List When No Tasks Match Expiration Time

**Purpose**:  
Verify that `findByStatusAndExpirationTimeBefore` returns an empty list when no `OrderExpirationTask` entities have an expiration time before the specified timestamp.

**Preconditions**:  
- The database is empty.
- A timestamp (`now`) is set to the current time.
- One `Order` entity with `CREATED` status is persisted.
- One `OrderExpirationTask` entity is created with `PENDING` status and an expiration time 1 hour after `now`.

**Steps**:  
1. Persist one `Order` entity.
2. Persist one `OrderExpirationTask` entity with the specified status and expiration time.
3. Flush the entity manager to ensure data is written to the database.
4. Call `findByStatusAndExpirationTimeBefore(TaskStatus.PENDING, now.minusHours(1))` to retrieve tasks.
5. Verify the results.

**Expected Outcome**:  
- The result list is empty.

## Test Scenario 3: Return Empty List When No Tasks Match Status

**Purpose**:  
Verify that `findByStatusAndExpirationTimeBefore` returns an empty list when no `OrderExpirationTask` entities have the specified `PENDING` status, even if the expiration time matches.

**Preconditions**:  
- The database is empty.
- A timestamp (`now`) is set to the current time.
- One `Order` entity with `CREATED` status is persisted.
- One `OrderExpirationTask` entity is created with `SENT` status and an expiration time 1 hour before `now`.

**Steps**:  
1. Persist one `Order` entity.
2. Persist one `OrderExpirationTask` entity with the specified status and expiration time.
3. Flush the entity manager to ensure data is written to the database.
4. Call `findByStatusAndExpirationTimeBefore(TaskStatus.PENDING, now)` to retrieve tasks.
5. Verify the results.

**Expected Outcome**:  
- The result list is empty.