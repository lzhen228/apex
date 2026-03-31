import { Routes, Route } from 'react-router-dom'
import TargetCombo from './views/TargetCombo'

function App() {
  return (
    <div className="w-full h-full bg-background">
      <Routes>
        <Route path="/" element={<TargetCombo />} />
        <Route path="/target-combo" element={<TargetCombo />} />
      </Routes>
    </div>
  )
}

export default App
