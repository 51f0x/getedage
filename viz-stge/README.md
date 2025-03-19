# StGe Explorer

A modern web application that visualizes the architecture and functionality of StGe (Static Kotlin Test Generator).

## Overview

StGe Explorer provides an interactive visualization of how StGe works, including:

- Architecture diagrams with step-by-step explanation
- Branch coverage visualization with examples
- Detailed overview of the entire test generation process

## Features

- **Interactive Architecture Flow**: Step through the entire StGe process from project scanning to test generation
- **Branch Coverage Visualization**: See how StGe identifies branches and creates tests for each condition
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Built with React, Tailwind CSS, and ShadCN UI components

## Technologies Used

- React 18
- React Router 7
- Tailwind CSS
- ShadCN UI
- Lucide React Icons
- Vite

## Getting Started

1. Clone the repository
2. Install dependencies:
   ```
   npm install
   ```
3. Start the development server:
   ```
   npm run dev
   ```
4. Open your browser to the URL shown in the terminal (usually http://localhost:5173)

## Build for Production

To create a production build:

```
npm run build
```

The output will be in the `dist` directory, which can be deployed to any static hosting service.

## Related Projects

- [StGe](https://github.com/yourusername/stge) - The core Static Kotlin Test Generator project
- [Example Project](https://github.com/yourusername/stge-example) - Example project with StGe-generated tests

## License

This project is licensed under the MIT License - see the LICENSE file for details. 