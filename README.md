# Coinverse

Coinverse is a personal financial management Android application designed to help users gain control over their budgeting and spending habits. With features like custom budget planning, receipt tracking, insightful analytics, and achievement badges, Coinverse provides an intuitive platform for financial responsibility and goal tracking.

---

## Demo Video

Watch a full walkthrough of Coinverse in action:  
**[Demo Video Link – Insert Here]**

---

## Purpose and Scope

Coinverse enables users to effectively manage their finances through:

- **User Registration & Login** using Firebase Authentication with username-based credentials  
- **Custom Budget Creation** on a weekly or monthly basis with category-specific allocations  
- **Expense Logging** with optional receipt attachment and real-time budget updates  
- **Category Management** using user-defined icons with cloud-local data synchronization  
- **Spending Insights** through dynamic charts and progress indicators  
- **User Profile Preferences** including password changes, dark mode toggle, and progression badges  

---

## Core Features

| Feature Area         | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| **Authentication**   | Secure sign-in and registration via Firebase Auth                          |
| **Budget Tracking**  | Create and monitor personalized budget goals                               |
| **Expense Logging**  | Add expenses with details and receipts, auto-updating relevant budgets     |
| **Categories**       | Create and manage spending categories with icon selection                  |
| **Analytics**        | View charts and statistics showing spending trends and budget progress     |
| **Profile Settings** | Change password, toggle dark mode, and earn badges for savings discipline  |

---

## Highlighted Features

### Dark Mode

Coinverse supports dark mode for reduced eye strain and improved battery life. Users can toggle between light and dark themes in the **Profile** screen.

### Account Badges

Users earn visual achievement badges based on their budgeting consistency, savings performance, and financial discipline. Badges are displayed on the **Profile** page to encourage progress and motivation.

### Financial Insights with Charts

The **Insights** view aggregates spending and budgeting data into intuitive charts and progress bars. Users can track:

- Category-specific expenditure
- Budget adherence over time
- Visual breakdown of savings versus spending

---

## Application Architecture

Coinverse follows a modular MVVM architecture for maintainability and scalability:

```
MainActivity (Authentication Entry Point)
└── LoginFragment (Firebase Auth)

ExpensesActivity (Main Dashboard)
├── LogExpense (Expense Entry)
├── BudgetGoals (Overview of Goals)
│   └── CreateBudgetActivity
├── Insights (Charts & Analytics)
├── Profile (Settings, Dark Mode, Badges)
├── CategoryDetailsActivity
└── AddCategoryActivity
```

---

## Data Persistence Strategy

A hybrid data architecture ensures seamless offline and online access:

- **Firebase Firestore** stores user profiles, budgets, and expenses
- **Room Database** caches category data and receipt images locally
- **Firebase Auth** handles user session management
- **CategoryRepository** and related mappers handle cloud-local synchronization

---

## Technology Stack

| Component             | Technology               | Purpose                                 |
|-----------------------|--------------------------|-----------------------------------------|
| UI Framework          | XML + Material Design     | Layout and styling                      |
| Language              | Kotlin                    | Application logic                       |
| Architecture Pattern  | MVVM                      | Modular code separation                 |
| Local Storage         | Room Database             | Categories and receipts                 |
| Cloud Storage         | Firebase Firestore        | Persistent cloud data                   |
| Authentication        | Firebase Auth             | Secure user login and sessions          |
| Navigation            | Bottom Navigation Bar     | Multi-activity access                   |

---

## Key User Flows

- **Authentication Flow**:  
  `MainActivity → LoginFragment → ExpensesActivity`

- **Expense Logging**:  
  `ExpensesActivity → LogExpense → optional AddCategoryActivity`

- **Budget Planning**:  
  `ExpensesActivity → BudgetGoals → CreateBudgetActivity`

- **Category Management**:  
  `ExpensesActivity → CategoryDetailsActivity / AddCategoryActivity`

---

## Development & Deployment

Coinverse uses Gradle for build automation. The app is modularized into feature-based packages with strict separation between UI, data, and business logic. Firebase integration eliminates the need for backend server maintenance.

Refer to `Build.gradle` for dependency management and configuration.

---

## License

This project is for academic use and demonstration purposes. For any reuse or distribution, please contact the project owner.
