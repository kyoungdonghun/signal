import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { TickerGridPage } from './pages/TickerGridPage';
import { TickerDetailPage } from './pages/TickerDetailPage';
import { RunDetailPage } from './pages/RunDetailPage';
import { TrackRecordPage } from './pages/TrackRecordPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<TickerGridPage />} />
          <Route path="/ticker/:ticker" element={<TickerDetailPage />} />
          <Route path="/ticker/:ticker/:runId" element={<RunDetailPage />} />
          <Route path="/track-record" element={<TrackRecordPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
